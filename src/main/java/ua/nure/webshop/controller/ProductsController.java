package ua.nure.webshop.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua.nure.webshop.domain.*;
import ua.nure.webshop.service.CartService;
import ua.nure.webshop.service.CategoriesService;
import ua.nure.webshop.service.ParametersService;
import ua.nure.webshop.service.ProductService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/")
public class ProductsController {

    @Value("${default.number.of.products.on.page}")
    private int defaultProductQuantity;
    private final ProductService productService;
    private final CategoriesService categoriesService;
    private final ParametersService parametersService;
    private final CartService cartService;

    public ProductsController(ProductService productService, CategoriesService categoriesService, ParametersService parametersService, CartService cartService) {
        this.productService = productService;
        this.categoriesService = categoriesService;
        this.parametersService = parametersService;
        this.cartService = cartService;
    }

    @GetMapping()
    public String index(Model model,
                        @RequestParam("page") Optional<Integer> page,
                        @RequestParam("size") Optional<Integer> size) {

        Page<Products> productsPage = productService.findAllProducts(setPageRequest(page, size));
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("productPage", productsPage);

        Iterable<Categories> categories = categoriesService.findAllCategories();
        model.addAttribute("categories", categories);

        setPageNumbersInModel(productsPage, model);

        return "products/products";
    }

    @GetMapping("/products/{categoryName}")
    public String findAllProductsByCategory(@PathVariable String categoryName,
                                            Model model,
                                            @RequestParam("page") Optional<Integer> page,
                                            @RequestParam("size") Optional<Integer> size,
                                            @RequestParam(name = "diagonals", required = false) Optional<List<String>> diagonalParams,
                                            @RequestParam(name = "resolutions", required = false) Optional<List<String>> resolutionsParams,
                                            @RequestParam(name = "memorySizes", required = false) Optional<List<String>> memorySizesParams,
                                            @RequestParam(name = "flashMemorySizes", required = false) Optional<List<String>> flashMemorySizesParams,
                                            @RequestParam(name = "batteryCapacities", required = false) Optional<List<String>> batteryCapacitiesParams,
                                            @RequestParam(name = "capacities", required = false) Optional<List<String>> capacitiesParams,
                                            @RequestParam(name = "colors", required = false) Optional<List<String>> colorsParams,
                                            @RequestParam(name = "cpus", required = false) Optional<List<String>> cpusParams,
                                            @RequestParam(name = "displayTypes", required = false) Optional<List<String>> displayTypesParams,
                                            @RequestParam(name = "manufacturers", required = false) Optional<List<String>> manufacturers) {
        Parameters parameters = new Parameters(diagonalParams.orElse(new ArrayList()),
                resolutionsParams.orElse(new ArrayList()),
                memorySizesParams.orElse(new ArrayList()),
                flashMemorySizesParams.orElse(new ArrayList()),
                batteryCapacitiesParams.orElse(new ArrayList()),
                capacitiesParams.orElse(new ArrayList()),
                colorsParams.orElse(new ArrayList()),
                cpusParams.orElse(new ArrayList()),
                displayTypesParams.orElse(new ArrayList()),
                manufacturers.orElse(new ArrayList()));
        Page<Products> productsPage = productService.findProductsByCategoryAndCondition(parameters, categoryName, setPageRequest(page, size));
        if ("Computer".equals(categoryName)) {
            List<Computer> computers = (List<Computer>) (List<?>) productsPage.getContent();
            model.addAttribute("products", computers);
        }
        if ("Smartwatch".equals(categoryName)) {
            List<Smartwatch> smartwatches = (List<Smartwatch>) (List<?>) productsPage.getContent();
            model.addAttribute("products", smartwatches);
        }
        if ("Smartphone".equals(categoryName)) {
            List<Smartphone> smartphones = (List<Smartphone>) (List<?>) productsPage.getContent();
            model.addAttribute("products", smartphones);
        }
        System.out.println(productsPage.getContent());
        setPageNumbersInModel(productsPage, model);
        model.addAttribute("productPage", productsPage);
        List<Categories> categories = new ArrayList();
        categories.add(new Categories(categoryName));
        model.addAttribute("categories", categories);
        parametersService.setParametersToModel(model);

        return "products/products";
    }

    @GetMapping("/cart/{productID}")
    public String addToCart(@PathVariable String productID,
                            Model model,
                            @RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size) {
        cartService.addProductToCart(productID, session());
        return index(model, page, size);
    }

    private PageRequest setPageRequest(Optional<Integer> page, Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(defaultProductQuantity);
        return PageRequest.of(currentPage - 1, pageSize);
    }

    private Model setPageNumbersInModel(Page<Products> productsPage, Model model) {
        int totalPages = productsPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return model;
    }

    public HttpSession session() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }
}
